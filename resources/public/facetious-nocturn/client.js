namespace("facetious-nocturn.Client", {
    "gizmo-atheneum.namespaces.Ajax": "Ajax",
}, ({ Ajax }) => {
  const Client = function() {
    const queue = [];
    const interval = setInterval(() => {
      const req = queue.shift();
      if (req) {
        console.log({ queue: Array.from(queue), req });
        const { method, args } = req;
        Ajax[method].apply(null, args);
      }
    }, 1);
    var isClosed = false;
    const request = function(method, args) {
      if (!isClosed) {
        if (method != "get") {
          console.log({ method, args });
        }
        queue.push({ method, args });
      }
    }
    this.get = function(url, callbacks) {
      request("get", [url, callbacks]);
    }
    this.poll = function(url, delay, callbacks) {
      const pollInterval = setInterval(() => {
        request("get", [url, callbacks]);
      }, delay);
      return () => clearInterval(pollInterval);
    }
    this.post = function(url, payload, callbacks) {
      request("post", [url, callbacks]);
    }
    this.put = function(url, payload, callbacks) {
      request("put", [url, payload, callbacks]);
    }
    this.delete = function (url, callbacks) { 
      request("delete", [url, callbacks]);
    }
    this.close = function() {
      clearInterval(interval);
      isClosed = true;
    }
  }
  Client.request = function(...args) {
    Ajax.request.apply(null, args);
  }
  return Client;
});