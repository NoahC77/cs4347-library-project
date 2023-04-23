import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'
import Menu from './menu'

import { useState, createContext, useContext } from 'react'
import { Context, Authentication } from '../App'

function Login(props) {
  const { page, setPage } = useContext(Context)
  const { auth, setAuth } = useContext(Authentication)

  return (
    <>
      <Title>Login</Title>

      <Field editable={true} text1="Username:" text2={`${props.uname}`}/>
      <Field editable={true} text1="Password:" text2={`${props.pword}`}/>

      <Submit onClick={() => setAuth(!auth)}/>
    </>
  );
}

export default Login;
