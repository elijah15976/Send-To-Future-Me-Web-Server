let accountStyle = `#account:hover ~ #account-dropdown{
               display:block;
             }
             #account-dropdown:hover{
               display:block;
               cursor:pointer;
             }`;
let accountStyle2 = `#account:hover{
               cursor:pointer;
             }`;

function indexInit(){
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
}

function cookieNotif(){
  try{
    if(localStorage.getItem("cookie") != 1){
      alert("This website uses cookies.\nPress OK to continue");
      localStorage.setItem("cookie", 1);
    }
  }
  catch(err){
    alert("This website uses cookies.\nPress OK to continue");
    localStorage.setItem("cookie", 1);
  }
}

function pushOutput(json){
  let build = "";
  
  for(let i = 0; i < json.length; i++){
    build += `<div class="infoCard rounded-corner" onclick='recordPage(${JSON.stringify(json[i]).replace(/&#13;/g, `\\n`)})'>
                <h3>Sender: ${json[i].Email}</h3>
                <h3>Receiver: ${json[i].Recipient}</h3>
                <p>Subject: ${json[i].Sub}</p>
                <p>Message: ${json[i].Msg.replace(/&#13;/g, `\n`)}</p>
                <p>Date: ${json[i].Date}</p>
                <p>Time: ${json[i].Time}</p>`;
    if(json[i].Privacy == "0"){
      build += `  <p>Privacy: Public</p>`;
    }
    else if(json[i].Privacy == "1"){
      build += `  <p>Privacy: Private</p>`;
    }
    if(json[i].Status == "0"){
      build += `  <p>Status: Pending</p>`;
    }
    else if(json[i].Status == "1"){
      build += `  <p>Status: Sent</p>`;
    }
    build += `</div>`;
  }
  
  return build;
}

function recordPage(dataEntry){
  sessionStorage.setItem("passedData", JSON.stringify(dataEntry).replace(/&#13;/g, `\\n`));
  window.location.href = "/record";
}

function getUser(){
  $.ajaxSetup({async: false});
  data = $.getJSON("/secret-to-user").responseText;
  console.log(data);
  if(data != "success"){
    return data;
  }
  return "";
}

function logOut(){
  document.cookie = "logOut=1;Max-Age=1;SameSite=Lax;Path=/";
  $.ajaxSetup({async: false});
  data = $.getJSON("/secret-to-user").responseText;
  if(data == "success"){
    document.cookie = "secret=;Max-Age=1;Domain=send-to-future-me.elijah15976.repl.co;SameSite=Lax;Path=/";
    alert("You have successfully signed out");
    window.location.href = "/";
  }
  else{
    alert("Something went wrong trying to sign you out\nRefresh the page and try again");
  }
}