import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function Item(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Item</Title>

      <Field editable={true} text1="Name:" text2={`${props.name}`}/>
      <Field editable={false} text1="ID:" text2={`${props.itemid}`}/>
      <Field editable={false} text1="Stock:" text2={`${props.stock}`}/>
      <Field editable={true} text1="Minimum Stock:" text2={`${props.minStock}`}/>
      <Field editable={true} text1="Sell Price:" text2={`${props.sellPrice}`}/>

      <Submit/>

      <Delete/>
    </>
  );
}

export default Item;
