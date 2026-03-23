namespace("spyfall.Discussion", {}, () => {
  return function(props) {
    return <div className="d-flex flex-column align-items-center justify-content-center h-100">
      {props.role == "Spy" ? (<>
        <h1 className="mb-4">You are the Spy!</h1>
        <h3 className="mb-4">Locations:</h3>
        <ul className="mb-4">
          {props.locations.map((location, index) => <li key={index}><button onClick={() => props.onSelectLocation(location)}>{location}</button></li>)}
        </ul>
      </>) : (props.role ? (<>
        <h1 className="mb-4">You are the {props.role}!</h1>
        <h3 className="mb-4">Location: {props.location}</h3>
        <h3 className="mb-4">Other Locations:</h3>
        <ul className="mb-4">
          {props.locations.map((location, index) => <li key={index}>{location}</li>)}
        </ul>
      </>) : (<>
        <h1 className="mb-4">Waiting for host to assign roles...</h1>
      </>))}
    </div>
  }
});