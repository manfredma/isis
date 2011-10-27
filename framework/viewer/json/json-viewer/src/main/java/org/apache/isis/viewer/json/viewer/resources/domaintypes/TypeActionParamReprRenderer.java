/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;

public class TypeActionParamReprRenderer extends AbstractTypeFeatureReprRenderer<TypeActionParamReprRenderer, ObjectActionParameter> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.TYPE_ACTION_PARAMETER);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new TypeActionParamReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, Rel rel, ObjectSpecification objectSpecification, ObjectActionParameter objectActionParameter) {
        String typeFullName = objectSpecification.getFullIdentifier();
        final ObjectAction objectAction = objectActionParameter.getAction();
        String actionId = objectAction.getId();
        final String paramName = objectActionParameter.getName();
        String url = String.format("domainTypes/%s/actions/%s/params/%s", typeFullName, actionId, paramName);
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.TYPE_ACTION_PARAMETER, url)
                          .withId(deriveId(objectActionParameter));
    }

    public TypeActionParamReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public TypeActionParamReprRenderer with(ParentSpecAndFeature<ObjectActionParameter> specAndFeature) {
        super.with(specAndFeature);
        
        // done eagerly so can use as criteria for x-ro-follow-links
        representation.mapPut("id", deriveId());

        return this;
    }

    protected String deriveId() {
        return deriveId(getObjectFeature());
    }

    private static String deriveId(ObjectActionParameter objectActionParameter) {
        return objectActionParameter.getAction().getId() + "-" + objectActionParameter.getName();
    }


    @Override
    protected void addLinkSelfIfRequired() {
        if(!includesSelf) {
            return;
        }
        getLinks().arrayAdd( 
                newLinkToBuilder(getResourceContext(), Rel.SELF, getParentSpecification(), getObjectFeature()).build());
    }

    @Override
    protected void addLinkUpToParent() {
        ObjectAction parentAction = this.objectFeature.getAction();
        
        final LinkBuilder parentLinkBuilder = 
                TypeActionReprRenderer.newLinkToBuilder(resourceContext, Rel.UP, objectSpecification, parentAction);
        getLinks().arrayAdd(parentLinkBuilder.build());
    }

    @Override
    protected void addPropertiesSpecificToFeature() {
        representation.mapPut("name", getObjectFeature().getName());
        representation.mapPut("number", getObjectFeature().getNumber());
        representation.mapPut("optional", getObjectFeature().isOptional());
        final MaxLengthFacet maxLength = getObjectFeature().getFacet(MaxLengthFacet.class);
        if(maxLength != null && !maxLength.isNoop()) {
            representation.mapPut("maxLength", maxLength.value());
        }
    }
    
    @Override
    protected void addLinksSpecificToFeature() {
        final LinkBuilder linkBuilder = 
                DomainTypeReprRenderer.newLinkToBuilder(resourceContext, Rel.RETURN_TYPE, objectFeature.getSpecification());
        getLinks().arrayAdd(linkBuilder.build());
    }
    
    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();        
    }


}