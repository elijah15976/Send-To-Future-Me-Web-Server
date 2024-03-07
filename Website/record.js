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
  insertInfo();
}

function insertInfo(){
  data = JSON.parse(sessionStorage.getItem("passedData"));
  //console.log(data);
  let html = `<h2>Subject: ${data.Sub}</h2>
              <h3>Sender: ${data.Email}</h3>
              <h3>Receiver: ${data.Recipient}</h3>
              <p>Date: ${data.Date}</p>
              <p>Time: ${data.Time}</p>`;
  if(data.Privacy == "0"){
    html += `  <p>Privacy: Public</p>`;
  }
  else if(data.Privacy == "1"){
    html += `  <p>Privacy: Private</p>`;
  }
  html += `<p>Message: ${data.Msg}</p>`;
  if(data.Status == "0"){
    html += `  <p>Status: Pending</p>`;
  }
  else if(data.Status == "1"){
    html += `  <p>Status: Sent</p>`;
  }
  html += `<p>ID: ${data.ID}</p>`;
  
  document.getElementById("op").innerHTML = html;
}