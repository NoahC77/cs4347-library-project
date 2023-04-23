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
        <div className="w-full flex justify-center">
          <div
            className="
            h-[5vh] w-[70%] bg-[#2288BB] rounded-[5px] mb-[5vh]
            border-[2px] border-black
            text-white text-[3vh] grid grid-cols-3 [cursor:pointer]"
            onClick={() => setPage(<SuppItem
              itemid={elem.itemid}
              vendorPrice={elem.vendorPrice}
              vendorid={elem.vendorid}
            />)}
          >
            <div className="border-r-[2px] border-black text-center">
              Vendor: {elem.vendor_name}
            </div>
            <div className="text-center">
              Item: {elem.item_name}
            </div>
            <div className="border-l-[2px] border-black text-center">
              Quantity: {elem.quantity}
            </div>
          </div> 
        </div>
      )}
    </>
  );
}

export default Items;
