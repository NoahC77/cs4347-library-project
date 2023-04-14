import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import SuppItem from './supp-item'
import AddSuppItem from './add-supp-item'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      itemid:a,
      vendorPrice:100,
      vendorid:1000-a
    })
  }

  return itemList
}

function Items() {
  const { page, setPage } = useContext(Context)
  const [ suppItems, setSuppItems ] = useState(populateItems)

  return (
    <>
      <Title>Supplied Items</Title>

      <Search onAddClick={() => setPage(<AddSuppItem/>)}/>
      
      {suppItems.map( elem => 
        <Option 
          text1={elem.itemid}
          className1="col-span-2 text-center"
          className2="hidden"
          onClick={() => setPage(<SuppItem
            itemid={elem.itemid}
            vendorPrice={elem.vendorPrice}
            vendorid={elem.vendorid}
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
