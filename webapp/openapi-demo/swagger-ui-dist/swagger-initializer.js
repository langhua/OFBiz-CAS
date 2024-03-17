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
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: urlParam ? urlParam : "https://petstore.swagger.io/v2/swagger.json",
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
                logout: (oriAction) => (keys) => {
                  // logout oauth
                  console.log("Logout from following securities:", keys[0])
                  if (keys[0] == 'passwordLogin') {
                    console.log("Logout from passwordLogin");
                  }
                  fetch('https://localhost:8443/oauth/logout', {
                     headers: {
                        'Accept': 'application/json'
                     }
                  })
                  // remove cookies
                  removeCookies();
                  return oriAction(keys) // don't forget! otherwise, Swagger UI won't logout
                }
              }
            }
          }
        }
      }
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
