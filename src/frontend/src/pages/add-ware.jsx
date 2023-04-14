import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function AddWare(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Add Warehouse</Title>

      <Field editable={true} text1="Name:" text2={`${props.name}`}/>
      <Field editable={true} text1="ID:" text2={`${props.wareid}`}/>
      <Field editable={true} text1="Square Footage:" text2={`${props.sqft}`}/>
      <Field editable={true} text1="State:" text2={`${props.state}`}/>
      <Field editable={true} text1="City:" text2={`${props.city}`}/>
      <Field editable={true} text1="Zip Code:" text2={`${props.zip}`}/>
      <Field editable={true} text1="Street:" text2={`${props.street}`}/>
      <Field editable={true} text1="Apartment:" text2={`${props.apt}`}/>

      <Submit/>
    </>
  );
}

export default AddWare;
