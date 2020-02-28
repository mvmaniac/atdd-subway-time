package atdd.favorite.application;

import atdd.favorite.dao.FavoriteDao;
import atdd.favorite.domain.FavoritePath;
import atdd.favorite.domain.FavoriteStation;
import atdd.favorite.exception.BadRequestException;
import atdd.favorite.exception.ConflictException;
import atdd.path.dao.LineDao;
import atdd.path.dao.StationDao;
import atdd.path.domain.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static atdd.TestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteDao favoriteDao;

    @Mock
    private StationDao stationDao;

    @Mock
    private LineDao lineDao;

    @DisplayName("지하철역 즐겨찾기 등록을 해야 한다")
    @Test
    void mustSaveForStation() {
        given(stationDao.findById(anyLong())).willReturn(TEST_STATION);
        given(favoriteDao.saveForStation(any())).willReturn(TEST_FAVORITE_STATION);

        FavoriteStation favoriteStation = favoriteService.saveForStation(TEST_MEMBER, STATION_ID);
        Station station = favoriteStation.getStation();

        assertThat(favoriteStation).isNotNull();
        assertThat(station).isNotNull();
        assertThat(station.getName()).isEqualTo(STATION_NAME);
    }

    @DisplayName("경로 즐겨찾기 등록을 해야 한다")
    @Test
    void mustSaveForPath() {
        given(stationDao.findById(STATION_ID)).willReturn(TEST_STATION);
        given(stationDao.findById(STATION_ID_4)).willReturn(TEST_STATION_4);
        given(lineDao.findAll()).willReturn(List.of(TEST_LINE));
        given(favoriteDao.saveForPath(any())).willReturn(TEST_FAVORITE_PATH);

        FavoritePath favoritePath = favoriteService.saveForPath(TEST_MEMBER, STATION_ID, STATION_ID_4);
        Station sourceStation = favoritePath.getSourceStation();
        Station targetStation = favoritePath.getTargetStation();

        assertThat(sourceStation).isNotNull();
        assertThat(sourceStation.getName()).isEqualTo(STATION_NAME);
        assertThat(targetStation).isNotNull();
        assertThat(targetStation.getName()).isEqualTo(STATION_NAME_4);
    }

    @DisplayName("경로 즐겨찾기 등록 시 같은 역인지 확인해야 한다")
    @Test
    void mustCheckSameStation() {
        given(stationDao.findById(STATION_ID)).willReturn(TEST_STATION);
        given(stationDao.findById(STATION_ID)).willReturn(TEST_STATION);

        assertThrows(
                ConflictException.class,
                () ->  favoriteService.saveForPath(TEST_MEMBER, STATION_ID, STATION_ID),
                "same station conflict"
        );
    }

    @DisplayName("경로 즐겨찾기 등록 시 연결할 수 있는 역인지 확인해야 한다")
    @Test
    void mustCheckConnectStation() {
        given(stationDao.findById(STATION_ID)).willReturn(TEST_STATION);
        given(stationDao.findById(STATION_ID_22)).willReturn(TEST_STATION_22);
        given(lineDao.findAll()).willReturn(List.of(TEST_LINE));

        assertThrows(
                BadRequestException.class,
                () ->  favoriteService.saveForPath(TEST_MEMBER, STATION_ID, STATION_ID_22),
                "no exist path"
        );
    }

}