let num = 0; //0 = log in, 1 = register
function changeRegister(){
  document.getElementById("current_status").innerHTML = "Register";
  document.getElementById("current_status_button").innerHTML = "Register";
  document.getElementById("switcher").innerHTML = `Already have an account? <a id="switcher_button" onclick="changeLogIn()">Log In</a>`;
  num = 1;
}

function changeLogIn(){
  document.getElementById("current_status").innerHTML = "Log In";
  document.getElementById("current_status_button").innerHTML = "Log In";
  document.getElementById("switcher").innerHTML = `Don't have an account? <a id="switcher_button" onclick="changeRegister()">Create an Account</a>`;
  num = 0;
}

function logIn(){
  let email = document.getElementById("email_input").value.replace(/\s/g, '');
  if(email == ""){
    document.getElementById("error_place").innerHTML = "Please enter an Email";
    document.getElementById("error_place").style = `background-color:#f90006;padding:20px;`;
    return;
  }
  
  let password = document.getElementById("password_input").value;
  if(password == ""){
    document.getElementById("error_place").innerHTML = "Please enter a Password";
    document.getElementById("error_place").style = `background-color:#f90006;padding:20px;`;
    return;
  }

  if(document.getElementById("error_place").innerHTML != ""){
    document.getElementById("error_place").innerHTML = "";
    document.getElementById("error_place").style = `background-color:#ffffff;padding:0px;`;
  }

  $.ajaxSetup({async: false});
  $.post(("/log-in"),
    {"email": email,
     "password": password,
     "num": num
    },
    function(data, status){
      if(status == "success"){
        if(data == "Bad Parameters"){
         alert("Something went wrong \nDouble check everything is filled out");
        }
        else if(data == "Internal Error"){
          alert("We ran into an Internal Error. Refresh the page and Try Again");
        }
        else if(data == "Password Invalid"){
          alert("The Password provided is incorrect. Please Try Again");
        }
        else if(data == "Email Not Found"){
          alert("The Email provided is not registered. (Are you verified?) Check for typos or Create an Account");
        }
        else if(data == "Not a Valid Email"){
          alert("Please enter a valid email");
        }
        else if(data == "User Already Registered"){
          alert("The Email provided already has an account on this platform. Please log in instead");
        }
        else if(data == "Critical"){
          alert("Uh oh, we've encountered a critical system error.\nIf you see this, please report this to the website creator ASAP");
        }
        else if(data == "success"){
          alert("You have logged in. Welcome back");
          window.location.href = "/";
        }
        else if(data == "User created"){
          alert("You have successfully created an account. Please verify your email before logging in");
          window.location.href = "/";
        }
      }
      else{
       alert("Something went wrong \nPlease try again later...");
      }
    }
  );
  
  console.log(`${email} ${password} ${num}`);
}