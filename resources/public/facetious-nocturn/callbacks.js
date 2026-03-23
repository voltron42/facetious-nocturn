namespace("facetious-nocturn.Callbacks", {
}, () => {
  const callbacks = function(onSuccess, onError) {
    return {
      success: (responseText) => {
        onSuccess(JSON.parse(responseText));
      },
      failure: ({ requestedFile, status, statusText, responseText}) => {
        if (onError) {
          onError({ requestedFile, status, statusText, responseText });
        } else {
          console.error(`Request for ${requestedFile} failed with status ${status} ${statusText}: ${responseText}`);
        }
      },
      stateChange: ({ state, min, max }) => {
        console.log(`Request state changed: ${state} (${min} to ${max})`);
      }
    }
  };
  return callbacks;
});