/**
 * @license
 * Copyright 2019 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

foam.CLASS({
    package: 'foam.nanos.rope',
    name: 'ROPE',
    documentation: 'model represents a single cell in a rope matrix',

    ids: [ 'junctionDAOKey' ],

    // sourceModel and targetModel here are NOT Necessarily the same as in the relationship
    // a one-way ROPE relationship
    properties: [
      {
        name: 'sourceModel',
        class: 'Class',
        javaType: 'foam.core.ClassInfo',
      }, 
      {
        name: 'targetModel',
        class: 'Class',
        javaType: 'foam.core.ClassInfo',
      },
      {
        name: 'sourceDAOKey',
        class: 'String'
      },
      {
        name: 'cardinality',
        class: 'String'
      },
      {
        name: 'junctionModel',
        class: 'Class',
        javaType: 'foam.core.ClassInfo',
      },
      {
        name: 'junctionDAOKey',
        class: 'String'
      },
      {
        name: 'inverseName',
        class: 'String'
      },
      {
        name: 'relationshipImplies',
        class: 'Array',
        of: 'foam.nanos.rope.ROPEActions'
      },
      // The Key is the action user would like to perform on tgtObj, and the value is a list of actions on the srcObj that if the user is allowed to perform, grants the permission 
      // for the action on tgtObj
      {
        name: 'CRUD',
        class: 'Map',
        javaType: 'java.util.Map<foam.nanos.rope.ROPEActions, java.util.List<foam.nanos.rope.ROPEActions>>'
      },
      // is true if the targetModel is actually sourceModel in the relationship and vice versa
      {
        name: 'isInverse',
        class: 'Boolean'
      }
    ]
});