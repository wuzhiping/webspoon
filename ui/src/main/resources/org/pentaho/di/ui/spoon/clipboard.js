(function(){
  'use strict';

  rap.registerTypeHandler( "webSpoon.Clipboard", {
      factory : function( properties ) {
        this._widget = new webSpoon.Clipboard( properties );
        if ( rwt.client.Client._browserName === 'explorer' ) {
          var doc = rwt.widgets.base.ClientDocument.getInstance();
          doc.addEventListener( "keydown", this._widget._onKeyDown, this._widget );
        }
        return this._widget;
      },
      destructor : function () {
        if ( rwt.client.Client._browserName === 'explorer' ) {
          var doc = rwt.widgets.base.ClientDocument.getInstance();
          doc.removeEventListener( "keydown", this._widget._onKeyDown, this._widget );
        }
      },
      properties : [ "text" ],
      events : [ "paste", "copy", "cut" ]
  } );

  rwt.define( "webSpoon" );

  webSpoon.Clipboard = function ( properties ) {
    var x = document.createElement("INPUT");
    x.setAttribute( "id", "input-clipboard" );
    x.setAttribute( "remoteObjectId", properties.self );
    x.addEventListener( "paste", function( event ) {
      var text = '';
      if ( typeof event.clipboardData === 'undefined' ) {
        text = window.clipboardData.getData( 'Text' ); // IE
      } else {
        text = event.clipboardData.getData( 'text/plain' );
      }
      var remoteObject = rap.getRemoteObject( rap.getObject( this.getAttribute( 'remoteObjectid' ) ) );
      event.preventDefault();
      remoteObject.notify( "paste", { "text": text } );
      $.notify( 'paste', 'success' );
    }, this );
    x.addEventListener( "copy", function( event ) {
      var obj = rap.getObject( this.getAttribute( 'remoteObjectid' ) );
      var remoteObject = rap.getRemoteObject( obj );
      if ( typeof event.clipboardData === 'undefined' ) {
        window.clipboardData.setData( 'Text', obj.getText() ); // IE
      } else {
        event.clipboardData.setData( 'text/plain', obj.getText() );
      }
      if ( rwt.client.Client._browserName != 'explorer' ) {
        remoteObject.notify( "copy" );
        $.notify( 'copy', 'success' );
      }
      event.preventDefault();
    }, this );
    document.body.appendChild( x );
    /*
     * cut event cannot be invoked programmatically on IE11 for some reason, so capture ctrl+x then execute copy and notify cut.
     */
    x.addEventListener( "cut", function( event ) {
      var obj = rap.getObject( this.getAttribute( 'remoteObjectid' ) );
      var remoteObject = rap.getRemoteObject( obj );
      if ( typeof event.clipboardData === 'undefined' ) {
        window.clipboardData.setData( 'Text', obj.getText() ); // IE
      } else {
        event.clipboardData.setData( 'text/plain', obj.getText() );
      }
      event.preventDefault();
      remoteObject.notify( "cut" );
      $.notify( 'cut', 'success' );
    }, this );
  };

  webSpoon.Clipboard.prototype = {
    setText : function( text ) {
      this._text = text;
    },

    getText : function() {
      return this._text;
    },

    _onKeyDown : function( event ) {
      const keyName = event._valueDomEvent.key;

      if ( keyName === 'Control' ) {
        return;
      }

      if ( event._valueDomEvent.ctrlKey ) {
        var x = document.getElementById( "input-clipboard" );
        x.select();
        var remoteObject = rap.getRemoteObject( this );
        if ( keyName === 'c' ) {
          document.execCommand( 'copy' );
          remoteObject.notify( "copy" );
          $.notify( 'copy', 'success' );
        } else if ( keyName === 'x' ) {
          document.execCommand( 'copy' );
          remoteObject.notify( "cut" );
          $.notify( 'cut', 'success' );
        }
      }
    }
  };
}());