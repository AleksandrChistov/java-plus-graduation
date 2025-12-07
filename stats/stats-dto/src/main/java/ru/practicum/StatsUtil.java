package ru.practicum;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class StatsUtil {

    public static final String IP_ADDRESS_PATTERN = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)[.]){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static final LocalDateTime EPOCH_LOCAL_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    public String getIpAddressOrDefault(String ip) {
        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "0.0.0.0";
        }
    }

    public StatsParams buildStatsParams(List<String> uris, boolean unique) {
        return buildStatsParams(uris, unique, EPOCH_LOCAL_DATE_TIME);
    }

    public StatsParams buildStatsParams(List<String> uris, boolean unique, LocalDateTime startDate) {
        StatsParams params = new StatsParams();
        params.setStart(startDate);
        params.setEnd(LocalDateTime.now());
        params.setUris(uris);
        params.setUnique(unique);
        return params;
    }

    public Map<Long, Long> getViewsMap(List<StatsView> statsViews) {
        return statsViews.stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));
    }

    public Map<Long, Long> getConfirmedRequestsMap(List<Object[]> confirmedRequests) {
        return confirmedRequests.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

}
