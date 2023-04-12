import { useState, useContext } from 'react'
import { Context } from '../App'
import Option from '../components/option'
import Search from '../components/search'
import AddItem from './add-item'

function populateItems()
{
  var itemList = []
  const itemNum = 25

  for(let a = 1; a <= itemNum; a++)
  {
    itemList.push({name:`Screw ${a}`, stock:10})
  }

  return itemList
}

function POs() {
  const { page, setPage } = useContext(Context)
  const [ items, setItems ] = useState(populateItems)

  return (
    <>
      <div className="h-[5vh] text-center" onClick={() => setPage(<AddItem/>)}>Title</div>

      <Search/>
      
      { items.map( elem => <Option name={elem.name} stock={elem.stock} className2="text-right"/> ) }

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
