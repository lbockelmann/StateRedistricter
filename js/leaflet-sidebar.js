/*global L*/

L.Control.Sidebar = L.Control.extend( /** @lends L.Control.Sidebar.prototype */ {
  includes: (L.Evented.prototype || L.Mixin.Events),

  options: {
    position: 'left'
  },

  initialize: function (id, options) {
    var i, child;

    L.setOptions(this, options);

    this._sidebar = L.DomUtil.get(id);

    L.DomUtil.addClass(this._sidebar, 'sidebar-' + this.options.position);

    if (L.Browser.touch)
      L.DomUtil.addClass(this._sidebar, 'leaflet-touch');

    for (i = this._sidebar.children.length - 1; i >= 0; i--) {
      child = this._sidebar.children[i];
      if (child.tagName == 'DIV' &&
        L.DomUtil.hasClass(child, 'sidebar-content'))
        this._container = child;
    }

    this._tabitems = this._sidebar.querySelectorAll('ul.sidebar-tabs > li, .sidebar-tabs > ul > li');
    for (i = this._tabitems.length - 1; i >= 0; i--) {
      this._tabitems[i]._sidebar = this;
    }

    this._panes = [];
    this._closeButtons = [];
    for (i = this._container.children.length - 1; i >= 0; i--) {
      child = this._container.children[i];
      if (child.tagName == 'DIV' &&
        L.DomUtil.hasClass(child, 'sidebar-pane')) {
        this._panes.push(child);

        var closeButtons = child.querySelectorAll('.sidebar-close');
        for (var j = 0, len = closeButtons.length; j < len; j++)
          this._closeButtons.push(closeButtons[j]);
      }
    }
  },

  addTo: function (map) {
    var i, child;

    this._map = map;

    for (i = this._tabitems.length - 1; i >= 0; i--) {
      child = this._tabitems[i];
      var sub = child.querySelector('a');
      if (sub.hasAttribute('href') && sub.getAttribute('href').slice(0, 1) == '#') {
        L.DomEvent
          .on(sub, 'click', L.DomEvent.preventDefault)
          .on(sub, 'click', this._onClick, child);
      }
    }

    for (i = this._closeButtons.length - 1; i >= 0; i--) {
      child = this._closeButtons[i];
      L.DomEvent.on(child, 'click', this._onCloseClick, this);
    }

    return this;
  },

  removeFrom: function (map) {
    console.log('removeFrom() has been deprecated, please use remove() instead as support for this function will be ending soon.');
    this.remove(map);
  },

  remove: function () {
    var i, child;

    this._map = null;

    for (i = this._tabitems.length - 1; i >= 0; i--) {
      child = this._tabitems[i];
      L.DomEvent.off(child.querySelector('a'), 'click', this._onClick);
    }

    for (i = this._closeButtons.length - 1; i >= 0; i--) {
      child = this._closeButtons[i];
      L.DomEvent.off(child, 'click', this._onCloseClick, this);
    }

    return this;
  },

  open: function (id) {
    var i, child;

    for (i = this._panes.length - 1; i >= 0; i--) {
      child = this._panes[i];
      if (child.id == id)
        L.DomUtil.addClass(child, 'active');
      else if (L.DomUtil.hasClass(child, 'active'))
        L.DomUtil.removeClass(child, 'active');
    }

    for (i = this._tabitems.length - 1; i >= 0; i--) {
      child = this._tabitems[i];
      if (child.querySelector('a').hash == '#' + id)
        L.DomUtil.addClass(child, 'active');
      else if (L.DomUtil.hasClass(child, 'active'))
        L.DomUtil.removeClass(child, 'active');
    }

    this.fire('content', {
      id: id
    });

    if (L.DomUtil.hasClass(this._sidebar, 'collapsed')) {
      this.fire('opening');
      L.DomUtil.removeClass(this._sidebar, 'collapsed');
    }

    return this;
  },

  close: function () {
    for (var i = this._tabitems.length - 1; i >= 0; i--) {
      var child = this._tabitems[i];
      if (L.DomUtil.hasClass(child, 'active'))
        L.DomUtil.removeClass(child, 'active');
    }

    if (!L.DomUtil.hasClass(this._sidebar, 'collapsed')) {
      this.fire('closing');
      L.DomUtil.addClass(this._sidebar, 'collapsed');
    }

    return this;
  },

  _onClick: function () {
    if (L.DomUtil.hasClass(this, 'active'))
      this._sidebar.close();
    else if (!L.DomUtil.hasClass(this, 'disabled'))
      this._sidebar.open(this.querySelector('a').hash.slice(1));
  },

  _onCloseClick: function () {
    this.close();
  }
});

L.control.sidebar = function (id, options) {
  return new L.Control.Sidebar(id, options);
};