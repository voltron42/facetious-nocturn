namespace("spyfall.Voting", {}, () => {
  return function(props) {
    return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
      <h1 className="mb-4">Vote For Who You Think The Spy Is</h1>
      <ul className="mb-4">
        {Object.entries(props.playerNames).map(([name]) => {
          if (props.vote) {
            return <li key={name}>{name}</li>
          } else {
            return <li key={name}><button className="btn btn-outline-primary" onClick={() => props.onVote(name)}>{name}</button></li>
          }
        })}
      </ul>
    </div>);
  }
});
