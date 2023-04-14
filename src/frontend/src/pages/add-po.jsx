import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function AddPO(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Add Purchase Order</Title>

      <Field editable={true} text1="Order ID:" text2={`${props.orderid}`}/>
      <Field editable={true} text1="Quantity:" text2={`${props.quantity}`}/>
      <Field editable={true} text1="Price:" text2={`${props.price}`}/>
      <Field editable={true} text1="Date:" text2={`${props.date}`}/>

      <Submit/>
    </>
  );
}

export default AddPO;
