import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function SuppItem(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Supplied Item</Title>

      <Field editable={false} text1="ID:" text2={`${props.itemid}`}/>
      <Field editable={false} text1="Vendor ID:" text2={`${props.vendorid}`}/>
      <Field editable={false} text1="Vendor Price:" text2={`${props.vendorPrice}`}/>

      <Submit/>

      <Delete/>
    </>
  );
}

export default SuppItem;
