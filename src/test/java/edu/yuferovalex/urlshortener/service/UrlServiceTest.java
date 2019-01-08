package edu.yuferovalex.urlshortener.service;

import edu.yuferovalex.urlshortener.RankedUrlImpl;
import edu.yuferovalex.urlshortener.model.RankedUrl;
import edu.yuferovalex.urlshortener.model.Url;
import edu.yuferovalex.urlshortener.repository.UrlRepository;
import edu.yuferovalex.urlshortener.utils.Base62;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository repository;

    @InjectMocks
    private UrlService service;

    @Test
    public void shouldGenerateNewShortUrls() {
        doAnswer(invocationOnMock -> {
            Url url = invocationOnMock.getArgument(0);
            url.setId(1);
            return null;
        }).when(repository).save(any(Url.class));

        final String ACTUAL_SHORT_URL = service.generateShortUrl("https://kontur.ru");

        verify(repository).save(any(Url.class));
        assertThat(ACTUAL_SHORT_URL, is("/l/" + Base62.to(1)));
    }

    @Test
    public void shouldReturnOriginalUrlByShortLink() {
        final Url EXPECTED_URL = mock(Url.class);
        when(repository.findById(1))
                .thenReturn(Optional.of(EXPECTED_URL));

        final Url ACTUAL_URL = service.getUrlByLink(Base62.to(1));

        assertSame(EXPECTED_URL, ACTUAL_URL);
        verify(repository).findById(1);
    }

    @Test
    public void shouldIncreaseRedirects() {
        Url urlMock = mock(Url.class);

        service.increaseRedirects(urlMock);

        verify(urlMock).increaseRedirects();
        verify(repository).save(urlMock);
    }

    @Test(expected = LinkNotFoundException.class)
    public void getOriginalUrlShouldThrowIfLinkNotPresentedInRepository() {
        when(repository.findById(1))
                .thenReturn(Optional.empty());

        service.getUrlByLink(Base62.to(1));
    }

    @Test
    public void shouldReturnRankedUrlByShortLink() {
        final RankedUrl EXPECTED_URL = new RankedUrlImpl(1, 1, 0, "https://kontur.ru");
        when(repository.findByIdWithRank(1))
                .thenReturn(Optional.of(EXPECTED_URL));

        final RankedUrl ACTUAL_URL = service.getRankedUrlByShortLink(Base62.to(1));

        assertEquals(EXPECTED_URL, ACTUAL_URL);
        verify(repository).findByIdWithRank(1);
    }

    @Test(expected = LinkNotFoundException.class)
    public void getRankedUrlByShortLinkShouldThrowIfLinkNotPresentedInRepository() {
        when(repository.findByIdWithRank(1))
                .thenReturn(Optional.empty());

        service.getRankedUrlByShortLink(Base62.to(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllRankedUrl() {
        final Pageable PAGE_REQUEST = PageRequest.of(1, 100);
        final Page<RankedUrl> PAGE = mock(Page.class);
        final Stream<RankedUrl> PAGE_DATA_STREAM = mock(Stream.class);
        final Iterable<RankedUrl> PAGE_DATA = mock(Iterable.class);

        when(repository.findAllWithRank(PAGE_REQUEST)).thenReturn(PAGE);
        when(PAGE.get()).thenReturn(PAGE_DATA_STREAM);
        when(PAGE_DATA_STREAM.collect(any())).thenReturn(PAGE_DATA);

        final Iterable<RankedUrl> ACTUAL_PAGE_DATA = service.getAllRankedUrl(PAGE_REQUEST);

        assertSame(PAGE_DATA, ACTUAL_PAGE_DATA);
        verify(repository).findAllWithRank(PAGE_REQUEST);
    }

    @Test(expected = WrongLinkException.class)
    public void getOriginalUrlShouldThrowIfWrongLinkFormat() {
        service.getUrlByLink("_asd_");
    }

    @Test(expected = WrongLinkException.class)
    public void getRankedUrlByShortLinkShouldThrowIfWrongLinkFormat() {
        service.getRankedUrlByShortLink("_asd_");
    }

}