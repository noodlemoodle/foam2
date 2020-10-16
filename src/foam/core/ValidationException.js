/**
 * @license
 * Copyright 2020 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */
foam.CLASS({
  package: 'foam.core',
  name: 'ValidationException',
  javaExtends: 'RuntimeException',
  implements: ['foam.core.Exception'],

  javaImports:[
    'java.lang.Exception'
  ],

  properties: [
    {
      class: 'Object',
      of: 'foam.core.PropertyInfo',
      name: 'propertyInfo'
    },
    {
      class: 'String',
      name: 'propName'
    },
    {
      class: 'String',
      name: 'errorMessage'
    }
  ],

  axioms: [
    {
      name: 'javaExtras',
      buildJavaClass: function(cls) {
        cls.extras.push(foam.java.Code.create({
        data: `
    public ValidationException(String message) {
      super(message);
    }

    public ValidationException(String message, Exception cause) {
      super(message, cause);
    }
            `
          }));
        }
      }
    ]
});
