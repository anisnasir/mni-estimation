package support;

import gnu.trove.map.hash.THashMap;
import topkgraphpattern.Pattern;

public class MapSupportCount implements SupportCount {
    THashMap<Pattern, Long> patternCount;
    public MapSupportCount() {
        patternCount = new THashMap<>();
    }

    public THashMap<Pattern, Long> getPatternCount() {
        return patternCount;
    }

    public void add(Pattern pattern) {
        Long count = patternCount.getOrDefault(pattern, 0l);
        patternCount.put(pattern, count+1);
    }

    public void remove(Pattern pattern) {
        Long count = patternCount.get(pattern);
        if(count != null) {
            if (count > 1)
                patternCount.put(pattern, count - 1);
            else
                patternCount.remove(pattern);
        }
    }
}
