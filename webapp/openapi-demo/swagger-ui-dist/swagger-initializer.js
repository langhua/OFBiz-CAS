function removeCookies() {
    var cookies = document.cookie.split(";");
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eqPos = cookie.indexOf("=");
        var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
        console.log("expire cookie: " + name);
    }
}

window.onload = function() {
  window.ui = SwaggerUIBundle({
    urls: [
      {name: 'OAuth2 APIs', url: '/openapi-demo/yaml/oauth2.yaml'},
      {name: 'OAuth2开放接口', url: '/openapi-demo/yaml/oauth2_zh.yaml'},
      {name: 'OpenAPI demo', url: '/openapi-demo/yaml/demo.yaml'},
      {name: '开放接口演示', url: '/openapi-demo/yaml/demo_zh.yaml'},
    ],
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl,
      function() {
        return {
          statePlugins: {
            auth: {
              wrapActions: {
                authorizeOauth2: (oriAction, system) => (payload) => {
                  payload.auth.code = ""
                  return oriAction(payload)
                },
                logout: (oriAction, system) => (payload) => {
                  // external logout
                  fetch(window.location.origin + '/oauth/logout')
                  removeCookies()
                  return oriAction(payload)
                }
              }
            }
          }
        }
      }
    ],
    layout: "StandaloneLayout",
  });

  window.ui.initOAuth({
    appName: 'OFBiz CAS OAuth2',
    clientId: 'SandFlower',
    clientSecret: 'sandflower',
  });
};
