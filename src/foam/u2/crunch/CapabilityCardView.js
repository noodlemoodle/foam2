/**
 * @license
 * Copyright 2020 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

foam.CLASS({
  package: 'foam.u2.crunch',
  name: 'CapabilityCardView',
  extends: 'foam.u2.View',

  implements: [ 'foam.mlang.Expressions' ],

  requires: [
    'foam.nanos.crunch.AgentCapabilityJunction',
    'foam.nanos.crunch.Capability',
    'foam.nanos.crunch.CapabilityJunctionStatus',
    'foam.nanos.crunch.UserCapabilityJunction',
    'foam.u2.crunch.Style',
    'foam.u2.Element',
    'foam.u2.view.ReadOnlyEnumView'
  ],

  imports: [
    'subject',
    'userCapabilityJunctionDAO'
  ],

  documentation: `
    A single card in a list of capabilities.
  `,

  properties: [
    {
      name: 'associatedEntity',
      expression: function(data, subject) {
        if ( ! data || ! subject ) return '';
        return data.associatedEntity === foam.nanos.crunch.AssociatedEntity.USER ? subject.user : subject.realUser;
      }
    },
    {
      name: 'cjStatus',
      documentation: `
        Stores the status of the capability feature and is updated when the user
        attempts to fill out or complete the CRUNCH forms.
      `,
      factory: function() {
        return foam.nanos.crunch.CapabilityJunctionStatus.AVAILABLE;
      }
    }
  ],

  methods: [
    function init() {
       this.SUPER();
       this.onDetach(this.userCapabilityJunctionDAO.on.put.sub(this.daoUpdate));
       this.daoUpdate();
    },

    function initE() {
      this.SUPER();
      var self = this;

      var style = self.Style.create();
      style.addBinds(self);

      self
        .addClass(style.myClass())
        .addClass(style.myClass('mode-circle'))
        .start()
          .addClass(style.myClass('icon-circle'))
          .style({
            'background-image': "url('" + self.data.icon + "')",
            'background-size': 'cover',
            'background-position': '50% 50%',
            'float': 'left'
          })
        .end()
        .start('span')
        .call(function () {
          var badgeWrapper = self.Element.create({ nodeName: 'SPAN' });
          this.add(badgeWrapper);
          var associatedEntity = self.data.associatedEntity === foam.nanos.crunch.AssociatedEntity.USER ? self.subject.user : self.subject.realUser;
          self.userCapabilityJunctionDAO.find(
            self.AND(
              self.OR(
                self.AND(
                  self.NOT(self.INSTANCE_OF(self.AgentCapabilityJunction)),
                  self.EQ(self.UserCapabilityJunction.SOURCE_ID, associatedEntity.id)
                ),
                self.AND(
                  self.INSTANCE_OF(self.AgentCapabilityJunction),
                  self.EQ(self.UserCapabilityJunction.SOURCE_ID, associatedEntity.id),
                  self.EQ(self.AgentCapabilityJunction.EFFECTIVE_USER, self.subject.user.id)
                )
              ),
              self.EQ(self.UserCapabilityJunction.TARGET_ID, self.data.id)
            )
          ).then(ucj => {
            var statusEnum =  foam.nanos.crunch.CapabilityJunctionStatus.AVAILABLE;
            if ( ucj ) {
              statusEnum = ucj.status;
              
              // Want approved to be displayed same as pending because this status denotes that
              // the ucj has passed review, but its prerequites are still to be reviewed and in pending status
              if ( statusEnum === foam.nanos.crunch.CapabilityJunctionStatus.APPROVED ) 
                statusEnum = foam.nanos.crunch.CapabilityJunctionStatus.PENDING;
            }
            var badge = self.ReadOnlyEnumView.create({
                data: statusEnum
              }).addClass(style.myClass('badge'))
              .style({ 'background-color': statusEnum.background });
            badgeWrapper.add(badge);
          });
        })
        .end()
        .start()
          .addClass(style.myClass('card-title'))
          .add(( self.data.name != '') ? self.data.name : self.data.id)
        .end()
        .start()
          .addClass(style.myClass('card-subtitle'))
          .select(self.data.categories.dao
            .where(this.EQ(foam.nanos.crunch.CapabilityCategory.VISIBLE, true)), function (category) {
              return this.E('span')
                .addClass(style.myClass('category'))
                .add(category.name);
          })
        .end()
        .start()
          .addClass(style.myClass('card-description'))
          .add(self.data.description)
        .end();
    }
  ],

  listeners: [
    {
      name: 'daoUpdate',
      code: function() {
        this.userCapabilityJunctionDAO.find(
          this.AND(
            this.EQ(this.UserCapabilityJunction.TARGET_ID, this.data.id),
            this.EQ(this.UserCapabilityJunction.SOURCE_ID, this.associatedEntity.id),
            this.OR(
              this.NOT(this.INSTANCE_OF(this.AgentCapabilityJunction)),
              this.AND(
                this.INSTANCE_OF(this.AgentCapabilityJunction),
                this.EQ(this.AgentCapabilityJunction.EFFECTIVE_USER, this.subject.user.id)
              )
            )
          )
        ).then(ucj => {
          if ( ucj ) this.cjStatus = ucj.status;
        });
      }
    }
  ]
});
