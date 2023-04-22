import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function AddSuppItem(props) {
  const { page, setPage } = useContext(Context)
  const arr = [
    ['val1', 'text1'],
    ['val2', 'text2']
  ]

  return (
    <>
      <Title>Add Supplied Item</Title>

      <Field editable={true} drop={true} options={arr} text1="ID:" text2={`${props.itemid}`}/>
      <Field editable={true} text1="Vendor ID:" text2={`${props.vendorid}`}/>
      <Field editable={true} text1="Vendor Price:" text2={`${props.vendorPrice}`}/>

      <Submit/>

    </>
  );
}

export default AddSuppItem;
