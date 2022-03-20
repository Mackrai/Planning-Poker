import React, {Component} from 'react'
import './App.css'

import Chat from "./Chat";

class App extends Component {
    constructor(props) {
        super(props)

        this.state = {
            sessions : []
        }
    }

    render() {
        return (
            <div className='App'>
                <Chat />
            </div>
        )
    }
}

export default App