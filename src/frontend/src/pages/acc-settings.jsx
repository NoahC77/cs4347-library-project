import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function AccSettings(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Account Settings</Title>

      <Field editable={true} text1="Username:" text2={`${props.uname}`}/>
      <Field editable={true} text1="Password:" text2={`${props.pword}`}/>

      <Field editable={true} text1="First Name:" text2={`${props.fname}`}/>
      <Field editable={true} text1="Last Name:" text2={`${props.lname}`}/>
      <Field editable={false} text1="Job Title:" text2={`${props.jobTitle}`}/>

      <Field editable={true} text1="State:" text2={`${props.state}`}/>
      <Field editable={true} text1="City:" text2={`${props.city}`}/>
      <Field editable={true} text1="Zip Code:" text2={`${props.zip}`}/>
      <Field editable={true} text1="Street:" text2={`${props.street}`}/>
      <Field editable={true} text1="Apartment:" text2={`${props.apt}`}/>

      <Submit/>
    </>
  );
}

export default AccSettings;
