namespace('planningPoker.Host',{
    'planningPoker.HostService':"HostService"
},({ HostService }) => {
    return class extends React.Component {
        constructor(props) {
            super(props);
            this.state = {};
        }
        render() {
            return <>{
                this.state.session ? <></> : <></>
            }</>;
        }
    }
});