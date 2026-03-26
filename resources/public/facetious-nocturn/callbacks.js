namespace("facetious-nocturn.Callbacks", {
}, () => {
  const callbacks = function(onSuccess, onError) {
    const onFailure = onError || (({ requestedFile, status, statusText, responseText, error }) => {
      if (error) {
        console.error({ error });
      } else {
        console.error(`Request for ${requestedFile} failed with status ${status} ${statusText}: ${responseText}`);
      }
    });
    return {
      success: (responseText) => {
        console.log({ message: `Request succeeded with response:`, responseText });
        var parsedResponse;
        try {
          parsedResponse = JSON.parse(responseText);
        } catch (error) {
          onFailure({ requestedFile: "unknown", status: "N/A", statusText: "Invalid JSON", responseText });
        }
        if (parsedResponse) {
          try {
            onSuccess(parsedResponse);
          } catch (error) {
            onFailure({ requestedFile: "unknown", status: "N/A", statusText: "Invalid JSON", responseText, error });
          }
        }
      },
      failure: onFailure,
      stateChange: ({ state, min, max }) => {
        console.debug(`Request state changed: ${state} (${min} to ${max})`);
      }
    }
  };
  return callbacks;
});