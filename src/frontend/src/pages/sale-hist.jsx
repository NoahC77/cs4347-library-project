import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import MakeSale from './make-sale'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({
      item:`Item ${a}`,
      itemid:a,
      saleid:1000 + a,
      date:'yesterday',
    })
  }

  return itemList
}

function SaleHist() {
  const { page, setPage } = useContext(Context)
  const [ items, setItems ] = useState(populateItems)

  return (
    <>
      <Title>Sale History</Title>

      <Search onAddClick={() => setPage(<MakeSale/>)}/>
      
      {items.map( elem => 
        <div className="w-full flex gap-[2%] justify-center">
          <div className="
            h-[10vh] w-[70%] bg-[#2288BB] rounded-[5px] mb-[5vh] border-[2px] border-black
            text-white text-[3vh] grid grid-rows-2
          ">
            <div className="grid grid-cols-2">
              <div className="w-full border-r-[2px] border-black pl-[2%]">
                {`Item: ${elem.item}`}
              </div>
              <div className="w-full border-black pl-[2%]">
                {`Item ID: ${elem.itemid}`}
              </div>
            </div>
            <div className="grid grid-cols-2">
              <div className="w-full border-r-[2px] border-t-[2px] border-t-[2px] border-black pl-[2%]">
                {`Sell Date: ${elem.date}`}
              </div>
              <div className="w-full border-t-[2px] border-black pl-[2%]">
                {`Sale ID: ${elem.saleid}`}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default SaleHist;
