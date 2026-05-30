/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.relations.topology;

import es.kosmo.core.model.relations.topology.impl.NoGapsTopologyRelation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.ContainsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.CrossesTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.DisjointTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.EqualsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.FilterTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.FullyCoveredByTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.IntersectsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.MustBeConnectedAtEndPointsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NoOverlapsOfAreasTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NoOverlapsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NoSelfIntersectionTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NoSelfOverlappingTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NonDuplicatedFeaturesTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.OnlyConnectedAtEndPointsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.OnlySinglePartFeaturesTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.OverlapsTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.TouchesTopologyRelation;
import org.saig.core.model.relations.topology.topologyRelationsImpl.WithinTopologyRelation;
import org.saig.jump.lang.I18N;

public class TopologyRelationsRepository {
    private static Logger LOGGER = Logger.getLogger(TopologyRelationsRepository.class);
    private static Map<String, Class<?>> relationIdToClassMap = new HashMap();

    static {
        TopologyRelationsRepository.registerDefaultTopologyRelations();
    }

    private static synchronized boolean registerDefaultTopologyRelations() {
        LOGGER.info((Object)I18N.getString(TopologyRelationsRepository.class, "registering-default-topology-relations"));
        try {
            TopologyRelationsRepository.registerTopologyRelation("contains", ContainsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Crosses", CrossesTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Disjoint", DisjointTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Equals", EqualsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Intersects", IntersectsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Overlaps", OverlapsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Touches", TouchesTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Within", WithinTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("No overlaps of areas", NoOverlapsOfAreasTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("No self intersection", NoSelfIntersectionTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("No self overlapping", NoSelfOverlappingTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Only connected at end points", OnlyConnectedAtEndPointsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Only single part features", OnlySinglePartFeaturesTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("no-topological-relation", FilterTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Fully covered by", FullyCoveredByTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Must be connected at end points", MustBeConnectedAtEndPointsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("no-overlaps", NoOverlapsTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("Non duplicated features", NonDuplicatedFeaturesTopologyRelation.class);
            TopologyRelationsRepository.registerTopologyRelation("No gaps", NoGapsTopologyRelation.class);
            LOGGER.info((Object)I18N.getString(TopologyRelationsRepository.class, "topologies-relations-registered"));
        }
        catch (IllegalArgumentException ex) {
            LOGGER.warn((Object)"", (Throwable)ex);
        }
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void registerTopologyRelation(String topologyRelationID, Class<?> topologyRelationClass) throws IllegalArgumentException {
        if (!TopologyRelationsRepository.containsTopologyRelation(topologyRelationID)) {
            if (!ITopologyRelation.class.isAssignableFrom(topologyRelationClass)) throw new IllegalArgumentException(I18N.getString(TopologyRelationsRepository.class, "the-topology-relation-class-must-implement-the-itopologyrelation-interface"));
            relationIdToClassMap.put(topologyRelationID, topologyRelationClass);
            LOGGER.info((Object)I18N.getMessage(TopologyRelationsRepository.class, "topology-relation-{0}-registered", new Object[]{topologyRelationID}));
            return;
        } else {
            LOGGER.warn((Object)I18N.getMessage(TopologyRelationsRepository.class, "the-topology-relation-{0}-has-been-already-registered", new Object[]{topologyRelationID}));
        }
    }

    public static ITopologyRelation getTopologyRelation(String topologyRelationId) throws Exception {
        ITopologyRelation relation;
        Class<?> relationClass = relationIdToClassMap.get(topologyRelationId);
        try {
            relation = (ITopologyRelation)relationClass.newInstance();
        }
        catch (IllegalAccessException e) {
            LOGGER.warn((Object)I18N.getMessage(TopologyRelationsRepository.class, "access-error-occured-while-registering-topology-relation-{0}", new Object[]{relationClass.getName()}));
            throw e;
        }
        catch (InstantiationException e) {
            LOGGER.warn((Object)I18N.getMessage(TopologyRelationsRepository.class, "could-not-instantiate-topology-relation-{0}", new Object[]{relationClass.getName()}));
            throw e;
        }
        return relation;
    }

    public static List<String> getTopologyRelations() {
        return new ArrayList<String>(relationIdToClassMap.keySet());
    }

    public static boolean containsTopologyRelation(String topologyRelationId) {
        return relationIdToClassMap.containsKey(topologyRelationId);
    }
}

