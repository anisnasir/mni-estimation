package support;

import gnu.trove.map.hash.THashMap;
import topkgraphpattern.Pattern;

public interface SupportCount {

    public THashMap<Pattern, Long> getPatternCount();

    public void add(Pattern pattern);

    public void remove(Pattern pattern);
}
