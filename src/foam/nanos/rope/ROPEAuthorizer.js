/**
 * @license
 * Copyright 2019 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

foam.CLASS({
  package: 'foam.nanos.rope',
  name: 'ROPEAuthorizer',
  implements: [ 'foam.nanos.auth.Authorizer' ],

  javaImports: [
    'foam.core.FObject',
    'foam.core.X',
    'foam.dao.ArraySink',
    'foam.dao.DAO',
    'foam.nanos.auth.AuthorizationException',
    'foam.nanos.auth.Authorizer',
    'foam.nanos.auth.User',
    'foam.nanos.rope.*',
    'java.lang.reflect.*',
    'java.util.ArrayList',
    'java.util.List',
    'java.util.Iterator',
    'java.util.Map',
    'static foam.mlang.MLang.*'
  ],

  properties: [
    {
      name: 'ropeDAO',
      class: 'Object',
      javaType: 'DAO'
    },
    {
      name: 'targetDAOKey',
      class: 'String',
      javaType: 'String'
    }
  ],

  methods: [
    {
      name: 'authorizeByRope',
      javaType: 'boolean',
      args: [
        { name: 'x', javaType: 'X' },
        { name: 'obj', javaType: 'FObject' },
        { name: 'targetDAOKey', javaType: 'String' },
        { name: 'crudKey', javaType: 'String' },
        { name: 'propertyKey', javaType: 'String' }
      ],
      javaCode: `    
      if ( getRopeDAO() == null ) setRopeDAO((DAO) x.get("ropeDAO"));
      List<ROPE> ropes = (List<ROPE>) ((ArraySink) ((DAO) getRopeDAO().where(AND(EQ(ROPE.TARGET_DAOKEY, targetDAOKey)))).select(new ArraySink())).getArray();

      for ( ROPE rope : ropes ) {
        if ( rope.check(x, obj, "", crudKey, propertyKey) ) return true;
      }
      return false; 
      `
    },
    {
      name: 'getPropertiesUpdated',
      javaType: 'List<String>',
      args: [
        { name: 'oldObj', javaType: 'FObject' },
        { name: 'obj', javaType: 'FObject' }
      ],
      documentation: `
      this function returns a list of the names of properties changed on update or set on create
      `,
      javaCode: `
      try {
        if ( oldObj == null ) oldObj = (obj.getClass()).newInstance();
      } catch ( Exception e ) {
        e.printStackTrace();
      }
      Map diff = oldObj.diff(obj);
      Iterator i = diff.keySet().iterator();
  
      List<String> propertyUpdates = new ArrayList<String>();
      while ( i.hasNext() ) {
        String propName = (String) i.next();
        propertyUpdates.add(propName);
      }
      return propertyUpdates;
      `
    },
    {
      name: 'authorizeOnCreate',
      type: 'Void',
      javaThrows: [
        'AuthorizationException'
      ],
      args: [
        {
          name: 'x',
          javaType: 'X'
        },
        {
          name: 'obj',
          javaType: 'FObject'
        }
      ],
      javaCode: `
      if ( ((User) x.get("user")).getId() == User.SYSTEM_USER_ID ) return;
  
      List<String> propertiesUpdated = getPropertiesUpdated(null, obj);
      for ( String property : propertiesUpdated ) {
        if ( ! authorizeByRope(x, obj, getTargetDAOKey(), "create", property ) ) {
          throw new AuthorizationException("You don't have permission to create this object");
        }
      }
      `
    },
    {
      name: 'authorizeOnRead',
      type: 'Void',
      javaThrows: [
        'AuthorizationException'
      ],
      args: [
        {
          name: 'x',
          javaType: 'X'
        },
        {
          name: 'obj',
          javaType: 'FObject'
        }
      ],
      javaCode: `
      if ( ((User) x.get("user")).getId() == User.SYSTEM_USER_ID ) return;
      if ( ! authorizeByRope(x, obj, getTargetDAOKey(), "read", "") ) throw new AuthorizationException("You don't have permission to read this object");
      `
    },
    {
      name: 'authorizeOnUpdate',
      type: 'Void',
      javaThrows: [
        'AuthorizationException'
      ],
      args: [
        {
          name: 'x',
          type: 'foam.core.X'
        },
        {
          name: 'oldObj',
          type: 'foam.core.FObject'
        },
        {
          name: 'newObj',
          type: 'foam.core.FObject'
        },
      ],
      javaCode: `
      if ( ((User) x.get("user")).getId() == User.SYSTEM_USER_ID ) return;
  
      List<String> propertiesUpdated = getPropertiesUpdated(oldObj, newObj);
      for ( String property : propertiesUpdated ) {
        if ( ! authorizeByRope(x, newObj, getTargetDAOKey(), "update", property ) ) {
          throw new AuthorizationException("You don't have permission to update this object");
        }
      }
      `
    },
    {
      name: 'authorizeOnDelete',
      type: 'Void',
      javaThrows: [
        'AuthorizationException'
      ],
      args: [
        {
          name: 'x',
          javaType: 'X'
        },
        {
          name: 'obj',
          javaType: 'FObject'
        }
      ],
      javaCode: `
      if ( ((User) x.get("user")).getId() == User.SYSTEM_USER_ID ) return;
      if ( ! authorizeByRope(x, obj, getTargetDAOKey(), "delete", "") ) throw new AuthorizationException("You don't have permission to delete this object");
      `
    },
    {
      name: 'checkGlobalRead',
      javaType: 'boolean',
      args: [
        {
          name: 'x',
          javaType: 'X'
        }
      ],
      javaCode: `
      return false;
      `
    },
    {
      name: 'checkGlobalRemove',
      javaType: 'boolean',
      args: [
        {
          name: 'x',
          javaType: 'X'
        }
      ],
      javaCode: `
      return false;
      `
    },
    {
      name: 'getPermissionPrefix',
      javaType: 'String',
      javaCode: `
      return "";
      `
    }
  ]
})