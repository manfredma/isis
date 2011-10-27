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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.AbstractObjectMemberReprRenderer.Mode;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.DomainTypeReprRenderer;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.TypeActionReprRenderer;
import org.codehaus.jackson.node.NullNode;

import com.google.common.collect.Lists;

public class ObjectActionReprRenderer extends AbstractObjectMemberReprRenderer<ObjectActionReprRenderer, ObjectAction> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.OBJECT_ACTION);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new ObjectActionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private ObjectActionReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    public JsonRepresentation render() {
        // id and memberType are rendered eagerly
        
        renderMemberContent();
        putDisabledReasonIfDisabled();
        
        if(mode.isStandalone()) {
            addParameterDetails();
        }

        return representation;
    }


    /////////////////////////////////////////////////////
    // details link
    /////////////////////////////////////////////////////

    /**
     * Mandatory hook method to support x-ro-follow-links
     */
    @Override
    protected void followDetailsLink(JsonRepresentation detailsLink) {
        RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_ACTION);
        final ObjectActionReprRenderer renderer = 
                (ObjectActionReprRenderer) factory.newRenderer(getResourceContext(), getLinkFollower(), JsonRepresentation.newMap());
        renderer.with(new ObjectAndAction(objectAdapter, objectMember))
                .usingLinkTo(linkTo)
                .asFollowed();
        detailsLink.mapPut("value", renderer.render());
    }

    /////////////////////////////////////////////////////
    // mutators
    /////////////////////////////////////////////////////

    @Override
    protected void addMutatorsIfEnabled() {
        if(usability().isVetoed()) {
            return;
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        final ActionSemantics semantics = ActionSemantics.determine(this.resourceContext, objectMember);
        
        final String mutator = semantics.getInvokeKey();
        final MutatorSpec mutatorSpec = mutators.get(mutator);
        
        addLinkFor(mutatorSpec);
    }

    
	private ObjectAdapter contributingServiceAdapter() {
    	ObjectSpecification serviceType = objectMember.getOnType();
    	List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
    	for (ObjectAdapter serviceAdapter : serviceAdapters) {
			if(serviceAdapter.getSpecification() == serviceType) {
				return serviceAdapter;
			}
		}
    	// fail fast
    	throw new IllegalStateException("Unable to locate contributing service");
	}

	
    @Override
    protected JsonRepresentation mutatorArgs(MutatorSpec mutatorSpec) {
        JsonRepresentation argMap = JsonRepresentation.newMap();
        List<ObjectActionParameter> parameters = objectMember.getParameters();
        for(int i=0; i<objectMember.getParameterCount(); i++) {
            argMap.mapPut(parameters.get(i).getName(), argValueFor(i)); 
        }
        return argMap;
    }

    @Override
    protected RepresentationType mutatorRepType() {
        final ObjectSpecification returnType = objectMember.getReturnType();
        if(returnType == null) {
            return RepresentationType.GENERIC;
        }
        if(returnType.containsFacet(CollectionFacet.class)) {
            return RepresentationType.LIST;
        }
        if(returnType.containsFacet(ValueFacet.class)) {
            return RepresentationType.SCALAR_VALUE;
        }
        return RepresentationType.DOMAIN_OBJECT;
    }
    
    private Object argValueFor(int i) {
        if(objectMember.isContributed()) {
            ObjectActionParameter actionParameter = objectMember.getParameters().get(i);
            if (actionParameter.getSpecification().isOfType(objectAdapter.getSpecification())) {
                return DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.OBJECT, objectAdapter).build();
            }
        }
        // force a null into the map
        return NullNode.getInstance();
    }


    /////////////////////////////////////////////////////
    // parameter details
    /////////////////////////////////////////////////////

    private ObjectActionReprRenderer addParameterDetails() {
    	List<Object> parameters = Lists.newArrayList();
		for (int i=0; i< objectMember.getParameterCount(); i++) {
			ObjectActionParameter param = objectMember.getParameters().get(i);
			parameters.add(paramDetails(param));
		}
		representation.mapPut("parameters", parameters);
		return this;
	}

	private Object paramDetails(ObjectActionParameter param) {
		final JsonRepresentation paramRep = JsonRepresentation.newMap();
		paramRep.mapPut("name", param.getName());
		paramRep.mapPut("num", param.getNumber());
		paramRep.mapPut("description", param.getDescription());
		Object paramChoices = choicesFor(param);
		if(paramChoices != null) {
			paramRep.mapPut("choices", paramChoices);
		}
		Object paramDefault = defaultFor(param);
		if(paramDefault != null) {
			paramRep.mapPut("default", paramDefault);
		}
		return paramRep;
	}

	private Object choicesFor(ObjectActionParameter param) {
		ObjectAdapter[] choiceAdapters = param.getChoices(objectAdapter);
		if(choiceAdapters == null || choiceAdapters.length == 0) {
			return null;
		}
        List<Object> list = Lists.newArrayList();
        for (final ObjectAdapter choiceAdapter : choiceAdapters) {
        	ObjectSpecification objectSpec = param.getSpecification();
        	list.add(DomainObjectReprRenderer.valueOrRef(resourceContext, choiceAdapter, objectSpec));
        }
        return list;
	}

	private Object defaultFor(ObjectActionParameter param) {
		ObjectAdapter defaultAdapter = param.getDefault(objectAdapter);
		if(defaultAdapter == null) {
			return null;
		}
    	ObjectSpecification objectSpec = param.getSpecification();
    	return DomainObjectReprRenderer.valueOrRef(resourceContext, defaultAdapter, objectSpec);
	}

	
	/////////////////////////////////////////////////////
	// extensions and links
    /////////////////////////////////////////////////////
	
     protected void addLinksToFormalDomainModel() {
         getLinks().arrayAdd(TypeActionReprRenderer.newLinkToBuilder(resourceContext, Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build());
     }
     
     protected void addLinksIsisProprietary() {
        if(objectMember.isContributed()) {
            ObjectAdapter serviceAdapter = contributingServiceAdapter();
            JsonRepresentation contributedByLink = DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.CONTRIBUTED_BY, serviceAdapter).build();
            getLinks().arrayAdd(contributedByLink);
        }
    }

     protected void putExtensionsIsisProprietary() {
         getExtensions().mapPut("actionType", objectMember.getType().name().toLowerCase());
         
         final ActionSemantics semantics = ActionSemantics.determine(resourceContext, objectMember);
         getExtensions().mapPut("actionSemantics", semantics.getName());
     }

}