namespace("facetious-nocturn.Callbacks", {
}, () => {
  const callbacks = function(onSuccess, onError) {
    const onFailure = onError || (({ requestedFile, status, statusText, responseText }) => {
      console.error(`Request for ${requestedFile} failed with status ${status} ${statusText}: ${responseText}`);
    });
    return {
      success: (responseText) => {
        console.log({ message: `Request succeeded with response:`, responseText });
        try {
          const parsedResponse = JSON.parse(responseText);
          onSuccess(parsedResponse);
        } catch (error) {
          onFailure({ requestedFile: "unknown", status: "N/A", statusText: "Invalid JSON", responseText });
        }
      },
      failure: onFailure,
      stateChange: ({ state, min, max }) => {
        console.log(`Request state changed: ${state} (${min} to ${max})`);
      }
    }
  };
  return callbacks;
});