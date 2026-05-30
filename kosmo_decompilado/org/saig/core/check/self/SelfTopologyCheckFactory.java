/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.check.self;

import com.vividsolutions.jump.workbench.model.Layer;
import org.saig.core.check.CheckingException;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.check.self.NoOverlapsOfAreasCheck;
import org.saig.core.check.self.NoSelfIntersectionCheck;
import org.saig.core.check.self.NoSelfOverlappingCheck;
import org.saig.core.check.self.OnlyConnectedAtEndPointsCheck;
import org.saig.core.check.self.OnlySinglePartCheck;
import org.saig.jump.lang.I18N;

public class SelfTopologyCheckFactory {
    public static final String NO_OVERLAPS_OF_AREAS_SELF_CHECK = "no_overlaps_of_areas";
    public static final String ONLY_CONNECTED_AT_ENDPOINTS_SELF_CHECK = "only_connected_at_endpoints";
    public static final String NO_SELF_OVERLAPPING_SELF_CHECK = "no_self_overlapping";
    public static final String NO_SELF_INTERSECTION_SELF_CHECK = "no_self_intersection";
    public static final String ONLY_SINGLE_PART_FEATURES_SELF_CHECK = "only_single_part_features";

    public static AbstractSelfTopologyCheck buildCheck(String selfCheck, Layer sourceLayer) throws CheckingException {
        if (selfCheck.equalsIgnoreCase(NO_OVERLAPS_OF_AREAS_SELF_CHECK)) {
            return new NoOverlapsOfAreasCheck(sourceLayer);
        }
        if (selfCheck.equalsIgnoreCase(ONLY_CONNECTED_AT_ENDPOINTS_SELF_CHECK)) {
            return new OnlyConnectedAtEndPointsCheck(sourceLayer);
        }
        if (selfCheck.equalsIgnoreCase(NO_SELF_OVERLAPPING_SELF_CHECK)) {
            return new NoSelfOverlappingCheck(sourceLayer);
        }
        if (selfCheck.equalsIgnoreCase(NO_SELF_INTERSECTION_SELF_CHECK)) {
            return new NoSelfIntersectionCheck(sourceLayer);
        }
        if (selfCheck.equalsIgnoreCase(ONLY_SINGLE_PART_FEATURES_SELF_CHECK)) {
            return new OnlySinglePartCheck(sourceLayer);
        }
        throw new CheckingException(I18N.getMessage("org.saig.core.check.self.SelfTopologyCheckFactory.Autocheck-type-unknown-{0}", new Object[]{selfCheck}));
    }
}

