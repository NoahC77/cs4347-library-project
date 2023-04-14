import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import PO from './po'
import AddPO from './add-po'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      orderid:a,
      quantity:10,
      price:100,
      date:"yesterday"
    })
  }

  return itemList
}

function POs() {
  const { page, setPage } = useContext(Context)
  const [ pos, setPOs ] = useState(populateItems)

  return (
    <>
      <Title>Purchase Orders</Title>

      <Search onAddClick={() => setPage(<AddPO/>)}/>
      
      {pos.map( elem => 
        <Option 
          text1={elem.date}
          className1="text-center col-span-2"
          onClick={() => setPage(<PO
            orderid={elem.orderid}
            quantity={elem.quantity}
            price={elem.price}
            date={elem.date}
          />)}
        /> 
      )}

      <div className="flex gap-[2vw] place-content-center place-items-center pb-[5vw]">
        Page {1} of {10} 
        <div className="
          h-[5vh] w-[5vw] bg-[#2288BB] rounded-[5px]
          text-white text-[4vh] leading-none text-center [cursor:pointer]
        ">
          {'>'}
        </div>
      </div>
    </>
  );
}

export default POs;
