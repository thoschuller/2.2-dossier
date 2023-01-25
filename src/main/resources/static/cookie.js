function setCookie(name, value){
document.cookie = name + "=" + value + "; path=/;";
}

function getCookie(name){
var cookies = document.cookie.split(";");
        for (let i = 0; i < cookies.length; i++) {
          var cookiePair = cookies[i].split("=");
          if (cookiePair[0].trim() == name){
            return cookiePair[1];
          }
        }
  return null;
}