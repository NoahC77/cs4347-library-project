import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Item from './item'
import AddItem from './add-item'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      name:`Item ${a}`,
      itemid:a,
      stock:1000 + a,
      minStock:10,
      sellPrice:100
    })
  }

  return itemList
}

function Items() {
  const { page, setPage } = useContext(Context)
  const [ items, setItems ] = useState(populateItems)

  return (
    <>
      <Title>Items</Title>

      <Search onAddClick={() => setPage(<AddItem/>)}/>
      
      {items.map( elem => 
        <Option 
          text1={elem.name}
          text2={elem.stock}
          className2="text-right"
          onClick={() => setPage(<Item
            name={elem.name}
            itemid={elem.itemid}
            stock={elem.stock}
            minStock={elem.minStock}
            sellPrice={elem.sellPrice}
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

export default Items;
