function init(){
  cookieNotif();
  let user = getUser();
  let styleSheet = document.createElement("style");
  if(user.length > 0){
    document.getElementById("account").innerHTML = user;
    styleSheet.innerText = accountStyle;
    document.head.appendChild(styleSheet);
    document.getElementById("email").value = user;
  }
  else{
    styleSheet.innerText = accountStyle2;
    document.head.appendChild(styleSheet);
    document.getElementById("account").addEventListener('click', function(event){window.location.href = "/login";});
    alert("You are not logged in, or you are not verified.\nPlease do so before trying to send a message");
  }
}

function postSongs(){  
  if((document.getElementById("email").value != "") &&
     (document.getElementById("recipient").value != "") &&
     (document.getElementById("date").value != "") &&
     (document.getElementById("time").value != "") &&
     (document.getElementById("sub").value != "") &&
     (document.getElementById("msg").value != "") &&
     (document.getElementById("privacy").value != "")){

    $.ajaxSetup({async: false});
    $.post(("/post-msg"),
      {"email": document.getElementById("email").value,
       "recipient": document.getElementById("recipient").value,
       "date": document.getElementById("date").value,
       "time": document.getElementById("time").value,
       "subject": document.getElementById("sub").value.replace(/\"/g, `&quot;`).replace(/\'/g, `&#39;`),
       "message": document.getElementById("msg").value.replace(/\n/g, `&#13;`).replace(/\"/g, `&quot;`).replace(/\'/g, `&#39;`),
       "privacy": document.getElementById("privacy").value
      },
        function(data, status){
         if(status == "success"){
           if(data == "Not Registered or Not Verified"){
             alert("Please Log in or Verify your account before sending a message");
           }
           else if(data == "Bad Parameters"){
             alert("Something went wrong \nDouble check everything is filled out");
           }
           else{
             alert("Your message has been scheduled");
             window.location.href = "/";
           }
         }
         else{
           alert("Something went wrong \nPlease try again later...");
         }
       }
    );
  }
  else{
    alert("Please fill out all parts");
  }
}