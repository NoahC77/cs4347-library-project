import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function MakeSale(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Make Sale</Title>

      <Field editable={true} text1="Name:" text2={`${props.name}`}/>
      <Field editable={true} text1="Item ID:" text2={`${props.itemid}`}/>
      <Field editable={true} text1="Warehouse ID:" text2={`${props.saleid}`}/>

      <Submit/>
    </>
  );
}

export default MakeSale;
