namespace("planning-poker.guest.PlanningPoker", {
  "facetious-nocturn.Guest": "GuestClient",
  "facetious-nocturn.Session": "SessionClient",
  "planning-poker.common.PlanningPoker": "PlanningPokerUI"
}, ({ GuestClient, SessionClient, PlanningPokerUI }) => {
  return class extends React.Component {
    constructor(props) {
      super(props);
      const { sessionId, guestId, name } = props;
      this.guest = GuestClient.init(sessionId, guestId);
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
      this.guest.poll((data) => {
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
      this.guest.poll((data) => {
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
      this.guest.update({ fromGuest: { vote } }, () => {
        this.setState({ vote });
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
      </div>;
    }
  }
});