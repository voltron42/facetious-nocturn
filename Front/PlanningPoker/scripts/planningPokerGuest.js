namespace('planningPoker.Guest',{
    'planningPoker.GuestService':'GuestService'
},({ GuestService }) => {
    return class extends React.Component {
        constructor(props) {
            super(props);
            this.state = {};
        }
        render() {
            return <>{
                this.state.session ? <>
                </> : <>
                </>
            }</>;
        }
    }
});