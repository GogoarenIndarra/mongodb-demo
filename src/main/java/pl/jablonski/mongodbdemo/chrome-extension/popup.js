document.addEventListener('DOMContentLoaded', function() {
  document.getElementById('sendData').addEventListener('click', function() {
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
      var currentUrl = tabs[0].url;
      fetch('http://localhost:8090/api/items/add-item', {
        method: 'POST',
        body: currentUrl,
        headers: {
          'Content-Type': 'application/json'
        }
      })
      .then(response => {
        if (response.ok) {
          document.getElementById('status').innerHTML = 'done!';
          return response.text();
        } else {
          throw new Error(`HTTP error! status code: ${response.status}`);
        }
      })
      .then(data => console.log(data))
      .catch(error => {
        if (error.message.includes('HTTP error!')) {
          // Display an error message to the user
          document.getElementById('status').innerHTML = 'ERROR sorry, something went wrong';
        } else {
          console.error('Error:', error);
        }
      });
    });
  });
});
