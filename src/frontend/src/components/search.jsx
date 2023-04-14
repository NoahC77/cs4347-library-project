function Search(props) {

  return (
    <>
      <div className="w-full flex gap-[2%] justify-center mb-[5vh]">
        <input placeholder="Search" className="
          h-[5vh] w-[56%] bg-[#EAEAEA] rounded-[5px] px-[2%]
          placeholder:text-gray
        "/>
        <div className="
          h-[5vh] w-[5%] bg-[#2288BB] rounded-[5px]
          flex place-content-center place-items-center [cursor:pointer]
        ">
          <img src="/search-icon.svg" className="h-[3vh] w-[3vw] object-contain"/>
        </div>
        <div onClick={props.onAddClick} className="
          h-[5vh] w-[5%] bg-[#22BB88] rounded-[5px]
          text-white text-[4vh] leading-none text-center [cursor:pointer]
        ">
          +
        </div>
      </div>
    </>
  )
}

export default Search;