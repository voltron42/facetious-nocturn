namespace("planning-poker.host.PlanningPoker", {
  "facetious-nocturn.Host": "HostClient",
  "facetious-nocturn.Session": "SessionClient",
  "planning-poker.common.PlanningPoker": "PlanningPokerUI"
}, ({ HostClient, SessionClient, PlanningPokerUI }) => {
  return class extends React.Component {
    constructor(props) {
      super(props);
      const { sessionId, hostId, name } = props;
      this.host = HostClient.init(sessionId, hostId);
      this.state = {
        view: "lobby", // lobby | voting | clearing
        users: [],
        name: name,
        votes: undefined,
        vote: undefined,
        clearing: false,
        guestNames: {},
        pollInterval: null
      };
    }

    componentDidMount() {
      this.pollLobby();
    }

    componentWillUnmount() {
      if (this.state.pollInterval) clearInterval(this.state.pollInterval);
    }

    pollLobby = () => {
      this.host.poll((data) => {
        const guestNames = (data.commonData && data.commonData.guestNames) || {};
        const allConfirmed = Object.values(guestNames).every(Boolean);
        const users = [data.commonData.hostName, ...Object.keys(guestNames)];
        if (allConfirmed) {
          // All guests confirmed, move to voting
          this.setState({
            view: "voting",
            users,
            guestNames,
            votes: data.commonData.votes,
            clearing: !!data.commonData.clearing
          }, this.pollVoting);
        } else {
          this.setState({
            users,
            guestNames
          });
        }
      });
    }

    pollVoting = () => {
      this.host.poll((data) => {
        const votes = data.commonData && data.commonData.votes;
        const clearing = !!(data.commonData && data.commonData.clearing);
        this.setState({
          votes,
          clearing,
          view: clearing ? "clearing" : "voting"
        });
      });
    }

    handleVote = (name, vote) => {
      this.host.update({ hostData: { vote } }, () => {
        this.setState({ vote });
      });
    }

    handleClear = () => {
      // Remove host vote, reset votes, set clearing
      this.host.update({
        hostData: {},
        commonData: { clearing: true, votes: Object.fromEntries(this.state.users.map(u => [u, undefined])) }
      }, () => {
        this.setState({ vote: undefined });
      });
    }

    render() {
      const { view, users, name, votes, vote, clearing } = this.state;
      if (view === "lobby") {
        return <div>
          <h2>Lobby</h2>
          <ul>
            {users.map((u) => <li key={u}>{u} {this.state.guestNames[u] === true ? <i className="fas fa-check text-success"></i> : <i className="fas fa-times text-danger"></i>}</li>)}
          </ul>
          <button onClick={() => this.host.copyInvite()}>Copy Invite Link</button>
        </div>;
      }
      return <div>
        <PlanningPokerUI
          users={users}
          name={name}
          votes={votes}
          vote={vote}
          clearing={clearing}
          onVote={this.handleVote}
        />
        {view === "voting" && votes && Object.values(votes).every(v => v !== undefined) && (
          <button className="btn btn-warning mt-3" onClick={this.handleClear}>Clear for next vote</button>
        )}
      </div>;
    }
  }
});