function Search(props) {

  return (
    <>
      <div className="w-[70vw] flex gap-[2vw] mx-[15vw]">
        <input placeholder="Search" className="
          h-[5vh] w-[60vw] bg-[#EAEAEA] rounded-[5px] mb-[5vh] px-[2vw]
          grid grid-cols-2 gap-[2vw]
          placeholder:text-gray
        ">
        </input>
        <div className="
          h-[5vh] w-[5vw] bg-[#2288BB] rounded-[5px]
          flex place-content-center place-items-center [cursor:pointer]
        ">
          <img src="/search-icon.svg" className="h-[3vh] w-[3vw] object-contain"/>
        </div>
        <div className="
          h-[5vh] w-[5vw] bg-[#2288BB] rounded-[5px]
          text-white text-[4vh] leading-none text-center [cursor:pointer]
        ">
          +
        </div>
      </div>
    </>
  )
}

export default Search;