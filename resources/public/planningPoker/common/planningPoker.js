namespace("planning-poker.common.PlanningPoker", {
  "planning-poker.common.Icons": "Icons"
}, ({ Icons }) => {
  return function(props) {
    return <div className="d-flex flex-column justify-content-center align-items-center">
      <h1>Plannning Poker</h1>
      { props.clearing ? <>
        <h2>Resetting for new vote ...</h2>
      </> :<>
        <ul>
          { props.users.map((name) => <li>{name}: {Icons.get((name == props.name)?props.vote:(props.votes || {})[name])}</li>)}
        </ul>
        {(!props.votes && !props.vote) && <div className="d-flex flex-wrap">
          { Icons.getAll().map((icon) => <button className="btn btn-outline-dark" onClick={() => props.onVote(props.name, icon)}>{Icons.get(icon)}</button>)}
        </div>}
      </>}
    </div>;
  }
});