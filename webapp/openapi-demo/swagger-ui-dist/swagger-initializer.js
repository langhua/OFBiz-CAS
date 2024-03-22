const queryString = window.location.search;
console.log(queryString);
const params = new URLSearchParams(queryString);
console.log(params);
const urlParam = params.get('url');
console.log(urlParam);

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
    // url: urlParam ? urlParam : "https://petstore.swagger.io/v2/swagger.json",
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
                logout: (oriAction, system) => (payload) => {
//                  const configs = system.getConfigs()
//                  const authorized = system.authSelectors.authorized()
//
//                  // code
//                  if (Array.isArray(payload)) {
//                    payload.forEach((authorizedName) => {
//                      console.log("authorizedName: ", authorizedName)
//                      if (authorizedName === 'codeLogin') {
//                        const auth = authorized.get(authorizedName, {})
//                        const code = auth.getIn(["code"])
//                        console.log("code: ", code)
//                      }
//                    })
//                  }

                  fetch('https://localhost:8443/oauth/logout')
                  // remove cookies
                  removeCookies()
                  return oriAction(payload); // don't forget! otherwise, Swagger UI won't logout
                }
              }
            }
          }
        }
      }
    ],
    layout: "StandaloneLayout"
  });

  window.ui.initOAuth({
    clientId: 'SandFlower',
    clientSecret: 'sandflower',
    // usePkceWithAuthorizationCodeGrant: 'false'
  });
};
