let data;
function init(){
  cookieNotif();
  let user = getUser();
  let styleSheet = document.createElement("style");
  if(user.length > 0){
    document.getElementById("account").innerHTML = user;
    styleSheet.innerText = accountStyle;
    document.head.appendChild(styleSheet);
  }
  else{
    styleSheet.innerText = accountStyle2;
    document.head.appendChild(styleSheet);
    document.getElementById("account").addEventListener('click', function(event){window.location.href = "/login";});
  }
  $.ajaxSetup({async: false});
  data = $.getJSON("/private-messages").responseJSON;

  console.log(data);

  if(data != "")
    document.getElementById("output").innerHTML = pushOutput(data);
  else
    alert("Log In to access your private messages");
}