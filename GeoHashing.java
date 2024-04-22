import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class GeoHashing {
    private static final String base32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private static final Map<Character, Integer> decodeMap;

    static {
        decodeMap = new HashMap<>();
        for (int i = 0; i < base32.length(); i++) {
            decodeMap.put(base32.charAt(i), i);
        }
    }

    public static String encode(double latitude, double longitude, int precs) {
        int[] bits = {16, 8, 4, 2, 1};
        int cd = 0;
        int bit = 0;
        List<Character> geoHash = new ArrayList<>();
        boolean even = true;
        double[] lonInt = {-180, 180};
        double[] latInt = {-90, 90};
        while (geoHash.size() < precs) {
            if (even) {
                double mid = (lonInt[0] + lonInt[1]) / 2;
                if (longitude > mid) {
                    cd |= bits[bit];
                    lonInt[0] = mid;
                } else {
                    lonInt[1] = mid;
                }
            } else {
                double mid = (latInt[0] + latInt[1]) / 2;
                if (latitude > mid) {
                    cd |= bits[bit];
                    latInt[0] = mid;
                } else {
                    latInt[1] = mid;
                }
            }
            even = !even;
            if (bit < 4) {
                bit++;
            } else {
                geoHash.add(base32.charAt(cd));
                cd = 0;
                bit = 0;
            }
        }
        StringBuilder result = new StringBuilder();
        for (char c : geoHash) {
            result.append(c);
        }
        return result.toString();
    }

    public static double[] decode(String geohash) {
        boolean isEven = true;
        double[] lonInt = {-180, 180};
        double[] latInt = {-90, 90};
        for (char c : geohash.toCharArray()) {
            int cd = decodeMap.get(c);
            for (int mask : new int[]{16, 8, 4, 2, 1}) {
                if (isEven) {
                    double mid = (lonInt[0] + lonInt[1]) / 2;
                    if ((cd & mask) != 0) {
                        lonInt[0] = mid;
                    } else {
                        lonInt[1] = mid;
                    }
                } else {
                    double mid = (latInt[0] + latInt[1]) / 2;
                    if ((cd & mask) != 0) {
                        latInt[0] = mid;
                    } else {
                        latInt[1] = mid;
                    }
                }
                isEven = !isEven;
            }
        }
        return new double[]{(latInt[0] + latInt[1]) / 2, (lonInt[0] + lonInt[1]) / 2};
    }

    public static Map<String, List<Map<String, double[]>>> generateGrid(Map<String, double[]> points, int precs) {
        Map<String, List<Map<String, double[]>>> grid = new HashMap<>();
        for (Map.Entry<String, double[]> entry : points.entrySet()) {
            double latitude = entry.getValue()[0];
            double longitude = entry.getValue()[1];

            double gridLat = Math.round(latitude * Math.pow(10, precs)) / Math.pow(10, precs);
            double gridLon = Math.round(longitude * Math.pow(10, precs)) / Math.pow(10, precs);

            String geohash = encode(gridLat, gridLon, precs);

            if (!grid.containsKey(geohash)) {
                grid.put(geohash, new ArrayList<>());
            }
            Map<String, double[]> pointMap = new HashMap<>();
            pointMap.put(entry.getKey(), new double[]{latitude, longitude});
            grid.get(geohash).add(pointMap);
        }
        return grid;
    }
}

